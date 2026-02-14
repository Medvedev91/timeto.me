import SwiftUI
import shared

struct HomeChecklistHintView: View {
    
    let hintUi: HomeVm.ChecklistHintUi
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        HStack {
            
            Button(
                action: {
                    hintUi.create(
                        dialogsManager: navigation,
                        onSuccess: { checklistDb in
                            navigation.sheet {
                                ChecklistFormItemsSheet(
                                    checklistDb: checklistDb,
                                    onDelete: {},
                                )
                            }
                        },
                    )
                },
                label: {
                    HStack {
                        
                        ZStack {
                            Image(systemName: "plus")
                                .foregroundColor(.black)
                                .font(.system(size: 13, weight: .semibold))
                        }
                        .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
                        .background(Circle().fill(.white))
                        .padding(.leading, HomeScreen__hPadding)
                        .padding(.trailing, HomeScreen__itemCircleMarginTrailing)
                        
                        Text(hintUi.title)
                            .foregroundColor(.white)
                            .font(.system(size: HomeScreen__primaryFontSize))
                            .frame(height: HomeScreen__itemHeight)
                    }
                }
            )
            
            Spacer()
            
            Button(
                action: {
                    hintUi.hide()
                },
                label: {
                    ZStack {
                        Image(systemName: "xmark")
                            .foregroundColor(.black)
                            .font(.system(size: 13, weight: .semibold))
                    }
                    .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
                    .background(Circle().fill(Color(.systemGray2)))
                    .padding(.trailing, HomeScreen__hPadding)
                }
            )
        }
    }
}
