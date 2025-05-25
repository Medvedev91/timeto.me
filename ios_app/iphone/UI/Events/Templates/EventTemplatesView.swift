import SwiftUI
import shared

struct EventTemplatesView: View {
    
    let onDone: (EventTemplateDb) -> Void
    
    var body: some View {
        VmView({
            EventTemplatesVm()
        }) { _, state in
            EventTemplatesViewInner(
                state: state,
                onDone: onDone
            )
        }
    }
}

private struct EventTemplatesViewInner: View {
    
    let state: EventTemplatesVm.State
    
    let onDone: (EventTemplateDb) -> Void

    ///

    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        ScrollView(.horizontal, showsIndicators: false) {
            
            HStack {
                
                Spacer()
                    .frame(width: H_PADDING_HALF)
                
                ForEach(state.templatesUi, id: \.eventTemplateDb.id) { templateUi in
                    
                    Button(
                        action: {
                            onDone(templateUi.eventTemplateDb)
                        },
                        label: {
                            ListButton(
                                text: templateUi.shortText
                            )
                        }
                    )
                    .contextMenu {
                        Button(
                            action: {
                                navigation.sheet {
                                    EventTemplateFormSheet(
                                        initEventTemplateDb: templateUi.eventTemplateDb
                                    )
                                }
                            },
                            label: {
                                Label("Edit", systemImage: "square.and.pencil")
                            }
                        )
                    }
                }
                
                Button(
                    action: {
                        navigation.sheet {
                            EventTemplateFormSheet(
                                initEventTemplateDb: nil
                            )
                        }
                    },
                    label: {
                        ListButton(
                            text: state.newTemplateText
                        )
                    }
                )
                
                Spacer()
                    .frame(width: H_PADDING_HALF)
            }
        }
    }
}

private struct ListButton: View {
    
    let text: String
    
    var body: some View {
        Text(text)
            .padding(.vertical, 4)
            .padding(.horizontal, H_PADDING_HALF)
            .foregroundColor(.blue)
            .font(.system(size: 15, weight: .light))
    }
}
