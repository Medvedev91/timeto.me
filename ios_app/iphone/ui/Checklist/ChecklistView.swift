import SwiftUI
import shared

private let checkboxSize = 21.0
private let checklistItemMinHeight = HomeScreen__ITEM_HEIGHT

struct ChecklistView: View {
    
    let checklistDb: ChecklistDb
    let maxLines: Int
    let onDelete: () -> Void

    var body: some View {
        VmView({
            ChecklistVm(checklistDb: checklistDb)
        }) { vm, state in
            ChecklistViewInner(
                vm: vm,
                state: state,
                maxLines: maxLines,
                onDelete: onDelete
            )
        }
    }
}

private struct ChecklistViewInner: View {
    
    let vm: ChecklistVm
    let state: ChecklistVm.State
    
    let maxLines: Int
    let onDelete: () -> Void
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    private var stateIconResource: String {
        let stateUi = state.checklistUI.stateUI
        if stateUi is ChecklistStateUi.Completed {
            return "checkmark.square.fill"
        }
        if stateUi is ChecklistStateUi.Empty {
            return "square"
        }
        if stateUi is ChecklistStateUi.Partial {
            return "minus.square.fill"
        }
        fatalError()
    }
    
    var body: some View {
        
        HStack(alignment: .top) {
            
            ScrollView {
                
                VStack {
                    
                    ForEach(state.checklistUI.itemsUI, id: \.item.id) { itemUi in
                        
                        Button(
                            action: {
                                itemUi.toggle()
                            },
                            label: {
                                
                                HStack {
                                    
                                    Image(systemName: itemUi.item.isChecked ? "checkmark.square.fill" : "square")
                                        .foregroundColor(c.white)
                                        .font(.system(size: checkboxSize, weight: .regular))
                                        .padding(.trailing, 10)
                                    
                                    Text(itemUi.item.text)
                                        .padding(.vertical, 4)
                                        .foregroundColor(.white)
                                        .font(.system(size: HomeScreen__PRIMARY_FONT_SIZE))
                                        .lineLimit(maxLines)
                                        .textAlign(.leading)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .frame(minHeight: checklistItemMinHeight)
                            }
                        )
                    }
                }
            }
            .scrollIndicators(.hidden)
            
            Button(
                action: {
                    state.checklistUI.stateUI.onClick()
                },
                label: {
                    
                    VStack {
                        
                        Image(systemName: stateIconResource)
                            .foregroundColor(Color.white)
                            .font(.system(size: checkboxSize, weight: .regular))
                            .frame(height: checklistItemMinHeight)
                        
                        Button(
                            action: {
                                navigation.sheet {
                                    ChecklistFormSheet(
                                        checklistDb: state.checklistUI.checklistDb,
                                        onDelete: {
                                            onDelete()
                                        }
                                    )
                                }
                            },
                            label: {
                                Image(systemName: "pencil")
                                    .foregroundColor(Color.white)
                                    .font(.system(size: checkboxSize, weight: .regular))
                                    .frame(height: checklistItemMinHeight)
                            }
                        )
                        
                        Spacer()
                    }
                }
            )
        }
        .padding(.horizontal, H_PADDING - 2)
    }
}
