import SwiftUI
import shared

struct TasksTabRepeatingsItemView: View {
    
    let repeatingUi: TasksTabRepeatingsVm.RepeatingUi
    let withTopDivider: Bool
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        ZStack(alignment: .top) {
            
            Button(
                action: {
                    navigation.sheet {
                        RepeatingFormSheet(
                            initRepeatingDb: repeatingUi.repeatingDb
                        )
                    }
                },
                label: {
                    
                    VStack {
                        
                        HStack {
                            Text(repeatingUi.dayLeftString)
                                .font(.system(size: 14, weight: .light))
                                .foregroundColor(.secondary)
                            
                            Spacer()
                            
                            Text(repeatingUi.dayRightString)
                                .font(.system(size: 14, weight: .light))
                                .foregroundColor(.secondary)
                        }
                        
                        HStack {
                            
                            Text(repeatingUi.listText)
                                .textAlign(.leading)
                            
                            Spacer()
                            
                            TriggersIconsView(
                                checklistsDb: repeatingUi.textFeatures.checklistsDb,
                                shortcutsDb: repeatingUi.textFeatures.shortcutsDb
                            )
                            
                            if (repeatingUi.repeatingDb.isImportant) {
                                Image(systemName: "flag.fill")
                                    .foregroundColor(.red)
                                    .padding(.leading, 8)
                            }
                        }
                        .padding(.top, 4)
                    }
                    .padding(.top, 10)
                    .padding(.bottom, 10)
                    .foregroundColor(.primary)
                }
            )

            if withTopDivider {
                Divider()
            }
        }
    }
}
